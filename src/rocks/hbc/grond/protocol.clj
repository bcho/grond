(ns rocks.hbc.grond.protocol)

(defprotocol Command
  (->name [_]
          "Get command name.")
  (execute! [_ ctx args]
            "Execute with context & arguments."))
