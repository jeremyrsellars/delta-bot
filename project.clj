(defproject delta-bot "0.1.0-SNAPSHOT"
  :description "FIXME: write this!"
  :url "http://example.com/FIXME"

  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.clojure/clojurescript "0.0-3196"]]

  :node-dependencies [[source-map-support "0.2.8"]
                      [node-xmpp-client "1.0.0-alpha20"]
                      [node-xmpp-core "1.0.0-alpha14"]
                      [node-stringprep "0.7.0"]]

  :plugins [[lein-cljsbuild "1.0.5"]
            [lein-npm "0.5.0"]]

  :source-paths ["src"]

  :clean-targets ["out"]

  :cljsbuild {
    :builds [{:id "delta-bot"
              :source-paths ["src"]
              :notify-command ["node" "run.js" "cmd" "time.cmd" "jid" ~(System/getenv "JABBER_ID") "pwd" ~(System/getenv "JABBER_PWD") "server" ~(System/getenv "JABBER_SERVER") "interval-seconds" "1" "stop-after-seconds" "3"]
              :compiler {
                :output-to "out/delta_bot.js"
                :output-dir "out"
                :target :nodejs
                :optimizations :none
                :source-map true}}
             {:id "release"
              :source-paths ["src"]
              :compiler {
                :output-to "out/release/delta_bot.js"
                :output-dir "out/release"
                :target :nodejs
                :optimizations :simple
                :source-map "out/release/delta_bot.js.map"}}]})
