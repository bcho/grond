# grond

[![Build Status](https://travis-ci.org/bcho/grond.svg)](https://travis-ci.org/bcho/grond)
[![Clojars Project](https://img.shields.io/clojars/v/rocks.hbc/grond.svg)](https://clojars.org/rocks.hbc/grond)

## Usage

```clojure
(ns my.cool.application
  (:require [rocks.hbc.grond.cli :as g]))

; Invoke as:
;
;   app ping --times 5
(g/defcommand ping
  :options [["-t" "--times TIMES"
             :default 0
             :parse-fn #(Integer/parseInt %)]]
  [ctx {{:keys [times]} :options}]
  (dotimes [n times]
    (println n)))

; Invoke as:
;
;   app a-plusb -a 10 -b 32
(g/defcommand a+b
  :name :a-plus-b
  :options [["-a" nil
             :default 0
             :parse-fn #(Integer/parseInt %)]
            ["-b" nil
             :default 0
             :parse-fn #(Integer/parseInt %)]]
  [ctx {{:keys [a b]} :options}]
  (println (+ a b)))

(g/defcommand cli
  :commands [ping a+b])

(defn -main
  [& args]
  (g/execute! cli args))
```

## License

MIT
