(ns foodtaste.governor
  "FoodTasteGovernor — the independent safety/scope layer gating every
  sample-intake-logging/session-scheduling proposal an advisor may
  make for a food and beverage taster/grader crew. The governor never
  dispatches hardware itself, NEVER performs the sensory evaluation
  itself (no robot can taste), NEVER records or proposes a
  quality-grade/pass-fail determination, and NEVER finalizes a
  food-safety-clearance decision — those are permanently out of this
  actor's scope and remain a human taster's exclusive, irreplaceable
  sensory judgment (README's 'Robotics premise': this actor
  coordinates SAMPLE-INTAKE LOGGING AND SCHEDULING ONLY — it never
  tastes, grades or clears food-safety itself). Modeled on
  cloud-itonami-isco-7412's elecmech.governor (closest available
  structural reference at scaffold time — same
  closed-allowlist + independently-verified-provenance +
  always-escalate-concern + content-based scope-exclusion shape;
  neither cloud-itonami-isco-7514 nor cloud-itonami-isco-7511 exist
  yet, confirmed via `gh api` 404 before this actor was scaffolded).

  HARD invariants (:hard? true, ALWAYS :hold, never overridable):
    1. taster provenance    — the taster must be independently
                               verified/registered before any action.
    2. facility provenance  — the facility must be independently
                               verified/registered before any action.
    3. no-actuation          — proposal :effect must be :propose (the
                               governor never dispatches hardware and
                               never performs any sensory/grading/
                               food-safety-clearance judgment itself;
                               it only gates what the advisor may
                               coordinate).
    4. closed op-allowlist   — only :log-sample-record,
                               :schedule-tasting-session,
                               :flag-anomaly-concern and
                               :coordinate-supply-order may ever be
                               proposed; anything else is refused.
    5. scope-excluded action — any proposal to perform the sensory
                               evaluation itself, to assign or finalize
                               a quality-grade assignment, to determine
                               or finalize a pass/fail determination,
                               or to finalize or authorize a
                               food-safety clearance, is a hard,
                               permanent block (checked both against
                               the proposed :op and, defense-in-depth,
                               against the proposal's :rationale text —
                               matched as full finalization/execution
                               ACTION phrases such as \"assign the
                               quality grade\" / \"determine the
                               pass/fail outcome\" / \"finalize the
                               food-safety clearance\", never as bare
                               nouns like \"quality\", \"grade\",
                               \"taste\" or \"safety\", so the check
                               can never self-trip on the advisor's own
                               routine rationale text, e.g. \"logged
                               sample intake record for taster …\" or
                               \"scheduled tasting session for sensory
                               evaluation panel …\" or \"…routed for
                               the human taster's attention\" — all
                               three legitimately contain those bare
                               nouns but none is a finalization action,
                               and all are exercised by
                               `governor-test/default-mock-advisor-proposals-never-self-trip-on-scope-exclusion`.
  ESCALATION invariants (:escalate? true, ALWAYS human sign-off
  regardless of confidence):
    6. :op :flag-anomaly-concern (a sample-condition anomaly — a
                               visible defect, an off-odor reported by
                               intake staff — always escalates to a
                               human taster, never auto-commits).
    7. :op :coordinate-supply-order above `supply-cost-threshold`.
    8. low confidence (< `confidence-floor`)."
  (:require [kotoba.lang.text :as str]
            [foodtaste.store :as store]))

(def confidence-floor 0.6)
(def supply-cost-threshold 2000)

(def allowed-ops
  #{:log-sample-record :schedule-tasting-session
    :flag-anomaly-concern :coordinate-supply-order})

;; Defense-in-depth: none of these ops are ever in `allowed-ops`
;; above, so they are already refused by the closed-allowlist check
;; below; they are named again here — as explicit finalization/
;; execution ACTIONS, never bare nouns — so a future allowlist edit
;; cannot silently re-open this specific out-of-scope path without
;; also touching this list. Four categories: performing the sensory
;; evaluation itself, assigning/finalizing a quality-grade assignment,
;; determining/finalizing a pass/fail determination, and finalizing/
;; authorizing a food-safety clearance — every one of these is a
;; human taster's exclusive, irreplaceable sensory judgment.
(def ^:private scope-excluded-ops
  #{:perform-sensory-evaluation
    :conduct-sensory-evaluation
    :assign-quality-grade
    :finalize-quality-grade-assignment
    :determine-pass-fail-outcome
    :finalize-pass-fail-determination
    :finalize-food-safety-clearance
    :authorize-food-safety-clearance})

;; Full finalization/execution ACTION phrases only — never bare nouns
;; ("quality", "grade", "taste", "sensory", "safety", "clearance",
;; "pass", "fail") — so this can never match inside the mock advisor's
;; own default rationale text (which legitimately contains those bare
;; nouns, e.g. "sensory evaluation panel" / "routed for the human
;; taster's attention"). See
;; `governor-test/default-mock-advisor-proposals-never-self-trip-on-scope-exclusion`.
(def ^:private scope-excluded-phrases
  ["perform the sensory evaluation"
   "conduct the sensory evaluation"
   "assign the quality grade"
   "finalize the quality grade assignment"
   "finalize the quality-grade assignment"
   "determine the pass/fail outcome"
   "determine the pass fail outcome"
   "finalize the pass/fail determination"
   "finalize the pass fail determination"
   "finalize the food-safety clearance"
   "finalize the food safety clearance"
   "authorize the food-safety clearance"
   "authorize the food safety clearance"])

(defn- contains-excluded-phrase? [s]
  (let [s (str/lower (or s ""))]
    (boolean (some #(str/includes? s %) scope-excluded-phrases))))

(defn- hard-violations [proposal taster-record facility-record]
  (let [{:keys [op rationale]} proposal]
    (cond-> []
      (nil? taster-record)
      (conj {:rule :no-taster
             :detail "未登録 taster への提案は不可（taster record は独立して検証・登録済み — certification status を含む — でなければならない）"})

      (nil? facility-record)
      (conj {:rule :no-facility
             :detail "未登録 facility への提案は不可（facility record は独立して検証・登録済みでなければならない）"})

      (not= :propose (:effect proposal))
      (conj {:rule :no-actuation
             :detail "effect は :propose のみ許可（governor は sensory evaluation/grading/food-safety-clearance 判断を直接実行しない）"})

      (not (contains? allowed-ops op))
      (conj {:rule :unknown-op
             :detail (str op " は closed op-allowlist に無い — 提案不可")})

      (or (contains? scope-excluded-ops op) (contains-excluded-phrase? rationale))
      (conj {:rule :scope-excluded-action
             :detail "sensory evaluation の実施・quality grade の確定・pass/fail 判定の確定・food-safety clearance の確定/許可は、この actor の権限外 — 常に永続ブロック（人間 taster の専権）"}))))

(defn check
  "Assess a proposal against `request`/`context`/`proposal` and a
  `store` implementing `foodtaste.store/Store`. Pure — never mutates
  the store, never dispatches a sensory-evaluation/grading/
  food-safety-clearance decision."
  [request _context proposal store]
  (let [taster-record (store/taster store (:taster-id request))
        facility-record (some->> (:facility-id proposal) (store/facility store))
        hard (hard-violations proposal taster-record facility-record)
        hard? (boolean (seq hard))
        conf (or (:confidence proposal) 0.0)
        low? (< conf confidence-floor)
        supply-order-over-threshold?
        (and (= :coordinate-supply-order (:op proposal))
             (number? (:cost proposal))
             (> (:cost proposal) supply-cost-threshold))
        always-risky? (or (= :flag-anomaly-concern (:op proposal))
                           supply-order-over-threshold?)]
    {:ok? (and (not hard?) (not low?) (not always-risky?))
     :violations hard
     :confidence conf
     :hard? hard?
     :escalate? (and (not hard?) (or low? always-risky?))}))
