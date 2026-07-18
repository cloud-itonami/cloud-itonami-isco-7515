(ns foodtaste.governor-test
  (:require [clojure.test :refer [deftest is testing]]
            [foodtaste.store :as store]
            [foodtaste.advisor :as advisor]
            [foodtaste.governor :as governor]))

(defn- fresh-store []
  (let [st (store/mem-store)]
    (store/register-taster! st {:taster-id "taster-1" :name "Kobo Yamada" :certified? true})
    (store/register-facility! st {:facility-id "FAC-1" :name "Kobo Tasting Lab" :max-supply-cost 2000})
    st))

(defn- op [op-kw & {:as extra}]
  (merge {:op op-kw :effect :propose :facility-id "FAC-1"
          :confidence 0.9 :stake :low}
         extra))

(def ^:private req {:taster-id "taster-1"})

(deftest ok-log-sample-record
  (let [st (fresh-store)
        v (governor/check req {} (op :log-sample-record) st)]
    (is (:ok? v))))

(deftest ok-schedule-tasting-session
  (let [st (fresh-store)
        v (governor/check req {} (op :schedule-tasting-session) st)]
    (is (:ok? v))))

(deftest ok-supply-order-at-threshold-boundary
  (testing "the supply-cost threshold escalate boundary is exclusive (over, not at)"
    (let [st (fresh-store)
          v (governor/check req {} (op :coordinate-supply-order :cost 2000) st)]
      (is (:ok? v)))))

(deftest hard-on-unregistered-taster
  (let [st (fresh-store)
        v (governor/check {:taster-id "nobody"} {} (op :log-sample-record) st)]
    (is (:hard? v))
    (is (some #(= :no-taster (:rule %)) (:violations v)))))

(deftest hard-on-unregistered-facility
  (let [st (fresh-store)
        v (governor/check req {} (op :log-sample-record :facility-id "FAC-ghost") st)]
    (is (:hard? v))
    (is (some #(= :no-facility (:rule %)) (:violations v)))))

(deftest hard-on-no-actuation-violation
  (let [st (fresh-store)
        v (governor/check req {} (assoc (op :log-sample-record) :effect :direct-write) st)]
    (is (:hard? v))
    (is (some #(= :no-actuation (:rule %)) (:violations v)))))

(deftest hard-on-op-outside-closed-allowlist
  (let [st (fresh-store)
        v (governor/check req {} (op :dispatch-taster) st)]
    (is (:hard? v))
    (is (some #(= :unknown-op (:rule %)) (:violations v)))))

(deftest hard-on-scope-excluded-op-perform-sensory-evaluation
  (testing "performing the sensory evaluation itself is a permanent block, never a routine op — no robot can taste"
    (let [st (fresh-store)
          v (governor/check req {} (assoc (op :perform-sensory-evaluation) :confidence 0.99) st)]
      (is (:hard? v))
      (is (some #(= :scope-excluded-action (:rule %)) (:violations v))))))

(deftest hard-on-scope-excluded-op-conduct-sensory-evaluation
  (testing "conducting the sensory evaluation itself is a permanent block, never a routine op — no robot can taste"
    (let [st (fresh-store)
          v (governor/check req {} (assoc (op :conduct-sensory-evaluation) :confidence 0.99) st)]
      (is (:hard? v))
      (is (some #(= :scope-excluded-action (:rule %)) (:violations v))))))

(deftest hard-on-scope-excluded-op-assign-quality-grade
  (testing "assigning a quality grade is a permanent block, never a routine op — the human taster's exclusive judgment"
    (let [st (fresh-store)
          v (governor/check req {} (assoc (op :assign-quality-grade) :confidence 0.99) st)]
      (is (:hard? v))
      (is (some #(= :scope-excluded-action (:rule %)) (:violations v))))))

(deftest hard-on-scope-excluded-op-finalize-quality-grade-assignment
  (testing "finalizing a quality-grade assignment is a permanent block, never a routine op"
    (let [st (fresh-store)
          v (governor/check req {} (assoc (op :finalize-quality-grade-assignment) :confidence 0.99) st)]
      (is (:hard? v))
      (is (some #(= :scope-excluded-action (:rule %)) (:violations v))))))

(deftest hard-on-scope-excluded-op-determine-pass-fail-outcome
  (testing "determining the pass/fail outcome is a permanent block, never a routine op"
    (let [st (fresh-store)
          v (governor/check req {} (assoc (op :determine-pass-fail-outcome) :confidence 0.99) st)]
      (is (:hard? v))
      (is (some #(= :scope-excluded-action (:rule %)) (:violations v))))))

(deftest hard-on-scope-excluded-op-finalize-pass-fail-determination
  (testing "finalizing the pass/fail determination is a permanent block, never a routine op"
    (let [st (fresh-store)
          v (governor/check req {} (assoc (op :finalize-pass-fail-determination) :confidence 0.99) st)]
      (is (:hard? v))
      (is (some #(= :scope-excluded-action (:rule %)) (:violations v))))))

(deftest hard-on-scope-excluded-op-finalize-food-safety-clearance
  (testing "finalizing the food-safety clearance is a permanent block, never a routine op"
    (let [st (fresh-store)
          v (governor/check req {} (assoc (op :finalize-food-safety-clearance) :confidence 0.99) st)]
      (is (:hard? v))
      (is (some #(= :scope-excluded-action (:rule %)) (:violations v))))))

(deftest hard-on-scope-excluded-op-authorize-food-safety-clearance
  (testing "authorizing the food-safety clearance is a permanent block, never a routine op"
    (let [st (fresh-store)
          v (governor/check req {} (assoc (op :authorize-food-safety-clearance) :confidence 0.99) st)]
      (is (:hard? v))
      (is (some #(= :scope-excluded-action (:rule %)) (:violations v))))))

(deftest hard-on-scope-excluded-rationale-assign-the-quality-grade-even-with-allowlisted-op
  (testing "defense-in-depth: a rationale that itself attempts to assign the quality grade is blocked even if the op looks routine"
    (let [st (fresh-store)
          v (governor/check req {} (assoc (op :schedule-tasting-session)
                                           :rationale "recommend we assign the quality grade now")
                             st)]
      (is (:hard? v))
      (is (some #(= :scope-excluded-action (:rule %)) (:violations v))))))

(deftest hard-on-scope-excluded-rationale-determine-the-pass-fail-outcome-even-with-allowlisted-op
  (testing "defense-in-depth: a rationale that itself attempts to determine the pass/fail outcome is blocked even if the op looks routine"
    (let [st (fresh-store)
          v (governor/check req {} (assoc (op :log-sample-record)
                                           :rationale "we should determine the pass/fail outcome right away")
                             st)]
      (is (:hard? v))
      (is (some #(= :scope-excluded-action (:rule %)) (:violations v))))))

(deftest hard-on-scope-excluded-rationale-finalize-the-food-safety-clearance
  (testing "defense-in-depth: a rationale attempting to finalize the food-safety clearance is blocked even if the op looks routine"
    (let [st (fresh-store)
          v (governor/check req {} (assoc (op :log-sample-record)
                                           :rationale "finalize the food-safety clearance and proceed")
                             st)]
      (is (:hard? v))
      (is (some #(= :scope-excluded-action (:rule %)) (:violations v))))))

(deftest always-escalates-anomaly-concern-even-at-high-confidence
  (testing "a sample-condition anomaly (visible defect, off-odor reported by intake staff) always requires human sign-off"
    (let [st (fresh-store)
          v (governor/check req {} (assoc (op :flag-anomaly-concern :anomaly-type :off-odor-reported)
                                           :confidence 0.99)
                             st)]
      (is (not (:hard? v)))
      (is (:escalate? v)))))

(deftest always-escalates-supply-order-above-threshold
  (let [st (fresh-store)
        v (governor/check req {} (assoc (op :coordinate-supply-order :cost 5000) :confidence 0.99) st)]
    (is (not (:hard? v)))
    (is (:escalate? v))))

(deftest escalates-low-confidence
  (let [st (fresh-store)
        v (governor/check req {} (assoc (op :log-sample-record) :confidence 0.3) st)]
    (is (not (:hard? v)))
    (is (:escalate? v))))

(deftest default-mock-advisor-proposals-never-self-trip-on-scope-exclusion
  (testing "the governor's scope-exclusion term list must never match the mock advisor's own default rationale text for any allowlisted op — CLAUDE.md's known self-tripping bug pattern (rationale legitimately contains bare nouns like 'quality'/'grade'/'taste'/'sensory'/'safety', but never the full finalization-action phrases)"
    (let [st (fresh-store)
          adv (advisor/mock-advisor)
          ops [:log-sample-record :schedule-tasting-session
               :flag-anomaly-concern :coordinate-supply-order]]
      (doseq [o ops]
        (let [request {:taster-id "taster-1" :op o :facility-id "FAC-1"
                        :stake :low :item "quality-grade candidate batch 44 lot A"
                        :anomaly-type :visible-defect :cost 500}
              proposal (advisor/-advise adv st request)
              v (governor/check request {} proposal st)]
          (is (not (:hard? v))
              (str o " proposal unexpectedly hard-blocked: " (:violations v))))))))
