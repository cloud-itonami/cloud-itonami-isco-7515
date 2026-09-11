(ns foodtaste.advisor
  "Food and Beverage Taster/Grader Advisor — proposing a
  sample-intake-logging/scheduling coordination operation (log a
  sample-intake/batch-identifier record, schedule a tasting session,
  flag a sample-condition anomaly, coordinate a tasting-supply order)
  from a taster roster, facility registration and anomaly-reporting
  policy. Swappable mock/llm; the advisor ONLY proposes —
  `foodtaste.governor` independently gates every proposal and always
  escalates anomaly concerns and above-threshold supply orders. The
  advisor never proposes to perform the sensory evaluation itself (no
  robot can taste), to directly record or finalize a
  quality-grade/pass-fail determination, or to finalize a
  food-safety-clearance decision — those stay permanently out of this
  actor's scope and remain a human taster's exclusive sensory
  judgment. Modeled on cloud-itonami-isco-7412's elecmech.advisor
  (closest available structural reference at scaffold time).

  A proposal: {:op :log-sample-record|:schedule-tasting-session|
               :flag-anomaly-concern|:coordinate-supply-order
               :effect :propose :taster-id str :facility-id str
               :cost number :anomaly-type kw :item str :stake kw
               :confidence n :rationale str}"
  (:require #?(:clj [clojure.edn :as edn] :cljs [cljs.reader :as edn])))

(defprotocol Advisor
  (-advise [advisor store request] "request -> proposal map"))

(defn- rationale-for [op taster-id facility-id anomaly-type]
  (case op
    :log-sample-record
    (str "logged sample intake record for taster " taster-id
         " at facility " facility-id)

    :schedule-tasting-session
    (str "scheduled tasting session for sensory evaluation panel at facility "
         facility-id)

    :flag-anomaly-concern
    (str "flagged " (name (or anomaly-type :anomaly)) " concern for taster "
         taster-id " at facility " facility-id
         " — routed for the human taster's attention")

    :coordinate-supply-order
    (str "coordinated supply order for taster " taster-id " at facility " facility-id)

    (str "proposed " (name op) " for taster " taster-id " at facility " facility-id)))

(defn- infer [_store {:keys [op stake taster-id facility-id cost anomaly-type item]
                       :as request}]
  {:op op
   :effect :propose
   :taster-id taster-id
   :facility-id facility-id
   :cost cost
   :anomaly-type anomaly-type
   :item item
   :stake (or stake :low)
   :confidence (case (or stake :low) :high 0.7 :medium 0.85 :low 0.95)
   :rationale (rationale-for op taster-id facility-id anomaly-type)})

(defn mock-advisor []
  (reify Advisor
    (-advise [_ store request] (infer store request))))

(def ^:private system-prompt
  "You are a food and beverage taster/grader sample-intake-logging and
   session-scheduling coordination advisor. Given a request, propose
   an :op (one of :log-sample-record, :schedule-tasting-session,
   :flag-anomaly-concern, :coordinate-supply-order), the :taster-id,
   :facility-id, and any :cost/:anomaly-type/:item fields, an honest
   :confidence and a :stake. Never propose an op outside this closed
   list, and never propose to perform the sensory evaluation itself
   (you cannot taste), to directly record or finalize a
   quality-grade/pass-fail determination, or to finalize a
   food-safety-clearance decision — those are always out of this
   actor's scope; it coordinates sample-intake logging and session
   scheduling only and never performs, records or finalizes any
   sensory/grading/food-safety-clearance judgment itself. Anomaly
   concerns always require human sign-off regardless of confidence.")

(defn- parse-proposal [content]
  (try
    (let [p (edn/read-string content)]
      (if (map? p)
        (assoc p :effect :propose)
        {:op :unknown :effect :propose :confidence 0.0 :stake :high
         :rationale "unparseable LLM response"}))
    (catch #?(:clj Exception :cljs js/Error) _
      {:op :unknown :effect :propose :confidence 0.0 :stake :high
       :rationale "LLM response parse failure"})))

(defn llm-advisor
  [chat-model model-generate-fn gen-opts]
  (reify Advisor
    (-advise [_ _store request]
      (let [msgs [{:role :system :content system-prompt}
                  {:role :user :content (str "operation request: " (pr-str request))}]
            resp (model-generate-fn chat-model msgs gen-opts)]
        (parse-proposal (:content resp))))))
