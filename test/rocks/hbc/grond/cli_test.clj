(ns rocks.hbc.grond.cli-test
  (:require [clojure.test :refer :all])
  (:require [rocks.hbc.grond
             [protocol :as p]
             [cli :as g]]))

(g/defcommand foo-cmd
  "Foo"
  :name :foo-cmd
  :options [["-f" "--foo FOO"]]
  [ctx _]
  :foo-cmd)

(g/defcommand bar-cmd
  "Bar"
  :name :named-cmd
  :commands [foo-cmd])

(deftest defcommand-test
  (testing "->name"
    (is (= (p/->name foo-cmd) :foo-cmd))
    (is (= (p/->name bar-cmd) :named-cmd)))

  (testing "->usage"
    (is (some? (re-find #"Foo" (p/->usage foo-cmd))))
    (is (some? (re-find #"Bar" (p/->usage bar-cmd))))
    (is (some? (re-find #"Foo" (p/->usage bar-cmd)))))

  (testing "execute!"
    (testing "execute! with basic command"
      (is (g/executed?
            (p/execute! foo-cmd {} []))))

    (testing "execute! with aggregative command"
      (is (g/executed?
            (p/execute! bar-cmd {} ["foo-cmd" "-f" "foobar"])))
      (is (not
            (g/executed?
              (p/execute! bar-cmd {} [])))))))
