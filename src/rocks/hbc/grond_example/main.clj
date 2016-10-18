(ns rocks.hbc.grond-example.main
  (:require [rocks.hbc.grond.cli :as g]))

(g/defcommand ping
  :options [["-t" "--times TIMES"
             :default 0
             :parse-fn #(Integer/parseInt %)]]
  [ctx {{:keys [times]} :options}]
  (dotimes [n times]
    (println n)))

(g/defcommand a+b
  :name :a-plus-b
  :options [["-a" nil
             :id :a
             :default 0
             :parse-fn #(Integer/parseInt %)]
            ["-b" nil
             :id :b
             :default 0
             :parse-fn #(Integer/parseInt %)]]
  [ctx {{:keys [a b]} :options :as p}]
  (println (+ a b)))

(g/defcommand cli
  :commands [ping a+b])

(defn -main
  [& args]
  (g/execute! cli args))
