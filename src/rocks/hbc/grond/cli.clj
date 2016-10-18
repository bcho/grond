(ns rocks.hbc.grond.cli
  (:require [clojure.tools.cli :as cli])
  (:require [rocks.hbc.grond.protocol :as p]))

(defrecord CliCommand [cmd-name cmd-options execute-fn]
  p/Command
  (->name [_] cmd-name)
  (execute! [_ ctx args]
    (execute-fn ctx (cli/parse-opts args cmd-options))))

(defn- execute-with-subcmds
  [sub-cmds]
  (fn [ctx parsed-options]
    (prn sub-cmds ctx parsed-options)
    nil))

(defn- parse-cmd-decl
  [cmd-symbol cmd-decl]
  (loop [cmd-name (keyword (name cmd-symbol))
         cmd-options []
         fn-args nil
         fn-body nil
         sub-cmds nil
         args cmd-decl]
    (if (empty? args)
      (do
        (assert cmd-name "`cmd-name` required")
        (assert cmd-options "`cmd-options` required")
        (if (some? sub-cmds)
          [cmd-name cmd-options (execute-with-subcmds sub-cmds)]
          [cmd-name cmd-options `(fn ~fn-args ~@fn-body)]))
      (let [arg (first args)
            args (rest args)]
        (cond
          (= :options arg)
          (recur cmd-name (first args) fn-args fn-body sub-cmds (rest args))

          (= :name arg)
          (recur (first args) cmd-options fn-args fn-body sub-cmds (rest args))

          (= :commands arg)
          (recur cmd-name cmd-options fn-args fn-body (first args) (rest args))

          :default
          (recur cmd-name cmd-options arg args sub-cmds '()))))))

(defmacro defcommand
  [cmd-symbol & cmd-decl]
  (let [[cmd-name cmd-options execute-fn]
        (parse-cmd-decl cmd-symbol cmd-decl)]
    `(def ~cmd-symbol
       (CliCommand. ~cmd-name ~cmd-options ~execute-fn))))

(defn execute!
  [^CliCommand cmd args]
  (p/execute! cmd {} args))
