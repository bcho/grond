(ns rocks.hbc.grond.protocol)

(defprotocol Command
  (->name [_]
          "Get command name.")
  (->usage [_]
           "Get command usage.")
  (execute! [_ ctx args]
            "Execute with context & arguments."))
