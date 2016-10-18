(ns rocks.hbc.grond-example.main
  (:require [rocks.hbc.grond.cli :as g]))

(g/defcommand ping
  "
  ping
  ====

  Print ping for `--times`.

  Usage
  -----

    lein -m rocks.hbc.grond-example.main ping --times 10
  "
  :options [["-t" "--times TIMES"
             :default 0
             :parse-fn #(Integer/parseInt %)]]
  [ctx {{:keys [times]} :options}]
  (dotimes [n times]
    (println "ping" n)))

(g/defcommand a+b
  "
  a-plus-b
  ========

  Calculate a + b.

  Usage
  -----

    lein -m rocks.hbc.grond-example.main a-plus-b --a 10 --b 32
  "
  :name :a-plus-b
  :options [["-a" "--a A"
             :default 0
             :parse-fn #(Integer/parseInt %)]
            ["-b" "--b B"
             :default 0
             :parse-fn #(Integer/parseInt %)]]
  [ctx {{:keys [a b]} :options :as p}]
  (println (+ a b)))

(g/defcommand cli
  "
  Grond Example
  ~~~~~~~~~~~~~
  "
  :commands [ping a+b g/usage])

(defn -main
  [& args]
  (g/execute! cli args))
