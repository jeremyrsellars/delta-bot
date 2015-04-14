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

(defn send-message! [client jid msg]
  (let [stanza (Element. "message" #js {"to" jid, "type" "chat"})]
    (-> stanza
        (.c "body")
        (.t msg))
    (.send client stanza)
    (println "Sending message:")
    (println msg)))

(defn send-status-message! [client presence-type]
  (let [stanza (Element. "presence" #js {"type" presence-type})]
    (-> stanza
        (.c "show")
        (.t "chat"))
    (.send client stanza)
    (println (str "sent status message: " presence-type))))

(let [args (-> js/process .-argv js->clj rest rest)
      args-map (apply hash-map args)]
  (println args-map)
  (defn get-arg [n default]
    (let [v (get args-map n)
          used (or v (str default))]
      (when-not v
        (println (str "Using default for " n ": " default)))
      used)))

(defn -main []
  (let [last-message (atom "")
        cmd (get-arg "cmd" "cmd /C dir")
        interval-seconds (-> (get-arg "interval-seconds" 3) js/parseInt)
        stop-after-seconds (-> (get-arg "stop-after-seconds" 30) js/parseInt)
        jid (get-arg "jid" "")
        to-jid (get-arg "jid" jid)
        pwd (get-arg "pwd" "")
        server (get-arg "server" "hipchat")
        client (->client (str jid "/bot") pwd server)
        send-if-different! (fn send-if-different! [msg]
                            (when-not (= @last-message msg)
                              (send-message! client jid msg)
                              (swap! last-message (constantly msg))))]
    (.on client "online"
      (fn online []
        (println "online!")
        (send-status-message! client "present")
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
                    (send-status-message! client "present"))
                  120000)]
          (js/setTimeout
            (fn shutdown! []
              (send-message! client jid "exiting at end of specified interval")
              (js/setTimeout (fn [](.end client)) 5000)
              (js/clearInterval interval-cookie)
              (js/clearInterval presence-interval-cookie))
            (* 1000 stop-after-seconds)))))))

(set! *main-cli-fn* -main)
