(ns foodtaste.store
  "SSoT for the ISCO-08 7515 Food and Beverage Tasters and Graders
  sample-intake-logging/session-scheduling coordination actor (itonami
  actor pattern, ADR-2607121000 / CLAUDE.md Actors section; README's
  'Robotics premise' — a sample-intake-logging/scheduling coordination
  robot performs sample intake/batch-identifier metadata logging,
  tasting-session scheduling and tasting-supply coordination for a
  human taster/grader crew under this advisor/governor pair, which
  never dispatches hardware itself, NEVER performs the sensory
  evaluation itself (no robot can taste), NEVER records or proposes a
  quality-grade/pass-fail determination, and NEVER finalizes a
  food-safety-clearance decision — those remain a human taster's
  exclusive, irreplaceable sensory judgment end to end). Modeled on
  cloud-itonami-isco-7412's elecmech.store (closest available
  structural reference at scaffold time — same closed-allowlist +
  independently-verified-provenance + always-escalate-concern shape;
  neither cloud-itonami-isco-7514 nor cloud-itonami-isco-7511 exist
  yet, confirmed via `gh api` 404 before this actor was scaffolded).

  Domain:

    taster    — a registered human food/beverage taster or grader
                {:taster-id :name :certified?}. `:certified?` is
                informational registered data (the taster's
                certification status as recorded at registration
                time) — the governor's provenance check only requires
                the taster record to exist (independently verified/
                registered before any action); it never lets a
                proposal override or bypass the certification
                requirement itself, and never lets a proposal
                substitute for, override or bypass the taster's own
                sensory judgment (see foodtaste.governor's
                scope-excluded-action rule).
    facility  — a registered tasting facility/lab account
                {:facility-id :name :max-supply-cost}.
                `:max-supply-cost` is an informational registered
                ceiling used only to decide whether a
                `:coordinate-supply-order` proposal escalates to human
                sign-off (the governor never blocks a within-threshold
                order outright; it only decides commit vs. escalate).
    record    — a committed operating record (a logged sample-intake
                entry, a scheduled tasting session, a flagged anomaly
                concern, or a coordinated supply order) — written ONLY
                via commit-record!. NEVER a taste/grade/pass-fail
                outcome or a food-safety-clearance decision — no such
                record shape exists anywhere in this actor.
    ledger    — append-only audit trail, commit or hold.")

(defprotocol Store
  (taster [s taster-id])
  (facility [s facility-id])
  (records-of [s taster-id])
  (ledger [s])
  (register-taster! [s t])
  (register-facility! [s f])
  (commit-record! [s record])
  (append-ledger! [s fact]))

(defrecord MemStore [a]
  Store
  (taster [_ taster-id] (get-in @a [:tasters taster-id]))
  (facility [_ facility-id] (get-in @a [:facilities facility-id]))
  (records-of [_ taster-id] (filter #(= taster-id (:taster-id %)) (:records @a)))
  (ledger [_] (:ledger @a))
  (register-taster! [s t]
    (swap! a assoc-in [:tasters (:taster-id t)] t) s)
  (register-facility! [s f]
    (swap! a assoc-in [:facilities (:facility-id f)] f) s)
  (commit-record! [s record]
    (swap! a update :records (fnil conj []) record) s)
  (append-ledger! [s fact]
    (swap! a update :ledger (fnil conj []) fact) s))

(defn mem-store
  ([] (mem-store {}))
  ([seed] (->MemStore (atom (merge {:tasters {} :facilities {} :records [] :ledger []}
                                    seed)))))
