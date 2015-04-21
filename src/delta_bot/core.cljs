(ns delta-bot.core
  (:require [cljs.nodejs :as nodejs]))

(nodejs/enable-util-print!)

(let [require nodejs/require]
  (try
    (.install (require "source-map-support"))
    (catch :default e
      (println "Couldn't install source-map-support")))
  (require "node-stringprep")
  (def xmpp (require "node-xmpp-client"))
  (def ltx (-> (require "node-xmpp-core") .-ltx))
  (def Element (.-Element ltx))
  (let [exec (.-exec (require "child_process"))]
    (defn execute [command callback]
      (exec command (fn [error stdout stderr] (callback stdout))))))

(def known-hosts
  {:hipchat "chat.hipchat.com"})

(defn resolve-host [kw-or-name]
  (or (known-hosts (keyword kw-or-name))
      (known-hosts kw-or-name)
      kw-or-name))

(defn ->client [jid password host]
  (xmpp. #js {:jid jid
              :password password
              :host (resolve-host host)
              :reconnect true}))

(defprotocol IMessageSender
  (send-message! [this msg])
  (join! [this])
  (assert-presence! [this]))

(letfn [(send-message*! [client type jid msg]
          (let [stanza (Element. "message" #js {"to" jid, "type" type})]
            (-> stanza
                (.c "body")
                (.t msg))
            (.send client stanza)))

        (send-status-message! [client presence-type]
          (let [stanza (Element. "presence" #js {"type" presence-type})]
            (-> stanza
                (.c "show")
                (.t "chat"))
            (.send client stanza)))

        (join-room! [client jid-slash-nick]
          (println (str "Joining " jid-slash-nick))
          (let [stanza (Element. "presence" #js {"to" jid-slash-nick})]
            (.c stanza "x" #js {"xmlns" "http://jabber.org/protocol/muc"})
            (.send client stanza)))]

  (defn chat-message-sender [client jid]
    (println (str "chat-message-sender   " jid))
    (reify IMessageSender
      (join! [this])
      (send-message! [this msg]
        (send-message*! client "chat" jid msg))
      (assert-presence! [this]
        (send-status-message! client "available"))))

  (defn groupchat-message-sender [client jid nickname]
    (let [jid-slah-nick (str jid "/" nickname)]
      (reify IMessageSender
        (join! [this]
          (join-room! client jid-slah-nick))
        (send-message! [this msg]
          (send-message*! client "groupchat" jid msg))
        (assert-presence! [this]
          (send-status-message! client "available"))))))

(let [args (-> js/process .-argv js->clj rest rest)
      args-map (apply hash-map args)]
  (defn get-arg [n default]
    (let [v (get args-map n)
          used (or v (if (nil? default) nil (str default)))]
      (when-not v
        (println (str "Using default for " n ": " default)))
      used)))

(defn get-room-args-or-nil []
  (let [room-jid (get-arg "room-jid" nil)
        my-nickname (get-arg "my-nickname" nil)]
    (when (and room-jid my-nickname)
      {:room-jid room-jid, :my-nickname my-nickname})))

(defn get-direct-args []
  (let [to-jid (or (get-arg "to-jid" nil)
                   (get-arg "my-jid" nil))]
    {:to-jid to-jid}))

(defn get-message-sender [client {:keys [to-jid room-jid my-nickname] :as args}]
  (if room-jid
    (groupchat-message-sender client room-jid my-nickname)
    (chat-message-sender client to-jid)))

(defn -main []
  (let [last-message (atom "")
        cmd (get-arg "cmd" "cmd /C dir")
        interval-seconds (-> (get-arg "interval-seconds" 3) js/parseInt)
        stop-after-seconds (-> (get-arg "stop-after-seconds" 30) js/parseInt)
        my-jid (get-arg "my-jid" "")
        pwd (get-arg "pwd" "")
        server (get-arg "server" "hipchat")
        client (->client (str my-jid "/bot") pwd server)
        room-args (get-room-args-or-nil)
        direct-args (get-direct-args)
        msg-sender (get-message-sender client (or room-args direct-args))
        send-if-different! (fn send-if-different! [msg]
                            (when-not (= @last-message msg)
                              (send-message! msg-sender msg)
                              (swap! last-message (constantly msg))))]

    (.on client "error"
      (fn error [e]
        (println (str "Error: " e))))

    (.on client "online"
      (fn online []
        (println "online!")
        (join! msg-sender)
        (assert-presence! msg-sender)
        (execute cmd send-if-different!)

        (let [interval-cookie
                (js/setInterval
                  (fn check-and-send []
                    (execute cmd send-if-different!))
                  (* 1000 interval-seconds))
              presence-interval-cookie
                ; send keepalive data or server will disconnect us after 150s of inactivity
                (js/setInterval
                  (fn maintain-presence []
                    (assert-presence! msg-sender))
                  120000)]
          (js/setTimeout
            (fn shutdown! []
              (send-message! msg-sender "exiting at end of specified interval")
              (js/setTimeout (fn [](.end client)) 5000)
              (js/clearInterval interval-cookie)
              (js/clearInterval presence-interval-cookie))
            (* 1000 stop-after-seconds)))))))

(set! *main-cli-fn* -main)
