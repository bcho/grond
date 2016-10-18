(ns rocks.hbc.grond.cli
  (:require [clojure.tools.cli :as cli])
  (:require [rocks.hbc.grond.protocol :as p]))

(def normalize-cmd-name (comp keyword name))

(defrecord CliCommand [cmd-name doc-fn execute-fn]
  p/Command
  (->name [_] cmd-name)
  (->usage [_]
    (doc-fn))
  (execute! [_ ctx args]
    (execute-fn ctx args)))

(defn- execute-with-subcmds
  [sub-cmds]
  `(fn [ctx# args#]
     (loop [args# args#]
       (if-not (empty? args#)
         (let [arg# (normalize-cmd-name (first args#))
               args# (rest args#)]
           (if-let [cmd# (some #(when (= (p/->name %) arg#) %) ~sub-cmds)]
             (p/execute! cmd# ctx# args#)
             (recur args#)))
         ctx#))))

(defn- execute-with-fn
  [cmd-options fn-args fn-body]
  `(fn [ctx# args#]
     (let [~(first fn-args) ctx#
           ~(second fn-args) (cli/parse-opts args# ~cmd-options)]
       ~@fn-body)
     (assoc ctx# ::executed? true)))

(defn- usage-with-subcmds
  [doc sub-cmds]
  `(fn [& _#]
     (let [sub-cmd-docs# (clojure.string/join
                           \newline (map #(p/->usage %) ~sub-cmds))]
       (str ~doc \newline sub-cmd-docs#))))

(defn- usage-with-fn
  [doc]
  `(constantly ~doc))

(defn- parse-cmd-decl
  [cmd-symbol cmd-decl]
  (let [has-doc? (string? (first cmd-decl))
        cmd-doc (if has-doc? (first cmd-decl) "")]
    (loop [cmd-name (normalize-cmd-name cmd-symbol)
           cmd-options []
           fn-args nil
           fn-body nil
           sub-cmds nil
           args (if has-doc? (rest cmd-decl) cmd-decl)]
      (if (empty? args)
        (do
          (assert cmd-name "`cmd-name` required")
          (assert cmd-options "`cmd-options` required")
          (if (some? sub-cmds)
            [cmd-name
             cmd-doc
             (usage-with-subcmds cmd-doc sub-cmds)
             (execute-with-subcmds sub-cmds)]
            [cmd-name
             cmd-doc
             (usage-with-fn cmd-doc)
             (execute-with-fn cmd-options fn-args fn-body)]))
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
              '())))))))

(defmacro defcommand
  "Define a command that accepts two arguments:

    - context: a hash map contains execution environment.
    - parsed-options: contains processed options parsed by `clojure.tools.cli`.

  Command can set with:

    - `:name` command name, use command symbol by default.
    - `:options` command options, refer `clojure.tools.cli` for details.
    - `:commands` list of sub-commands. Command body will be ignored
                  if this option is enabled.
  "
  [cmd-symbol & cmd-decl]
  (let [[cmd-name cmd-doc cmd-doc-fn execute-fn]
        (parse-cmd-decl cmd-symbol cmd-decl)]
    `(def ^{:doc ~cmd-doc} ~cmd-symbol
       (CliCommand. ~cmd-name ~cmd-doc-fn ~execute-fn))))

(defn show-usage
  "Display command usage."
  [^CliCommand cmd]
  (println (p/->usage cmd)))

(defn executed?
  [{executed? ::executed?}]
  (some? executed?))

(defn execute!
  "Execute a command with arguments."
  [^CliCommand cmd args]
  (let [ctx {::root-cmd cmd}]
    (when-not (executed? (p/execute! cmd ctx args))
      (show-usage cmd)
      (System/exit 1))))

(defcommand usage
  :name usage
  [{root-cmd ::root-cmd} _]
  (println (p/->usage root-cmd)))
