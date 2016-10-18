(ns rocks.hbc.grond.cli
  (:require [clojure.tools.cli :as cli])
  (:require [rocks.hbc.grond.protocol :as p]))

(def normalize-cmd-name (comp keyword name))

(defrecord CliCommand [cmd-name execute-fn]
  p/Command
  (->name [_] cmd-name)
  (execute! [_ ctx args]
    (execute-fn ctx args)))

(defn- execute-with-subcmds
  [sub-cmds]
  `(fn [ctx# args#]
     (loop [args# args#]
       (when-not (empty? args#)
         (let [arg# (normalize-cmd-name (first args#))
               args# (rest args#)]
           (if-let [cmd# (some #(do (when (= (p/->name %) arg#) %)) ~sub-cmds)]
             (p/execute! cmd# ctx# args#)
             (recur args#)))))))

(defn- execute-with-fn
  [cmd-options fn-args fn-body]
  `(fn [ctx# args#]
     (prn args#)
     (let [~(first fn-args) ctx#
           ~(second fn-args) (cli/parse-opts args# ~cmd-options)]
       ~@fn-body)))

(defn- parse-cmd-decl
  [cmd-symbol cmd-decl]
  (loop [cmd-name (normalize-cmd-name cmd-symbol)
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
          [cmd-name (execute-with-subcmds sub-cmds)]
          [cmd-name (execute-with-fn cmd-options fn-args fn-body)]))
      (let [arg (first args)
            args (rest args)]
        (cond
          (= :options arg)
          (recur
            cmd-name
            (first args)
            fn-args
            fn-body
            sub-cmds
            (rest args))

          (= :name arg)
          (recur
            (normalize-cmd-name (first args))
            cmd-options
            fn-args
            fn-body
            sub-cmds
            (rest args))

          (= :commands arg)
          (recur
            cmd-name
            cmd-options
            fn-args
            fn-body
            (first args)
            (rest args))

          :default
          (recur
            cmd-name
            cmd-options
            arg
            args
            sub-cmds
            '()))))))

(defmacro defcommand
  [cmd-symbol & cmd-decl]
  (let [[cmd-name execute-fn]
        (parse-cmd-decl cmd-symbol cmd-decl)]
    `(def ~cmd-symbol
       (CliCommand. ~cmd-name ~execute-fn))))

(defn execute!
  [^CliCommand cmd args]
  (p/execute! cmd {} args))
