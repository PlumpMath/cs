(ns ^:figwheel-always cs.core
  (:refer-clojure :exclude [==])
  (:require-macros [clara.macros :refer [defrule defquery defsession]])
  (:require [clara.rules :refer [insert retract fire-rules query insert! retract!]]
    ;[clara.tools.tracing :refer :all]
            [rum]))

(enable-console-print!)

(println "Edits to this text should show up in your developer console.")

; TODO: move views into their own file
(rum/defc greeting-view [greeting]
          [:h1 greeting])

(rum/defc members-view [members]
          [:h1 "Members"]
          [:ul (get-members)])

;; define your app data so that it doesn't get over-written on reload

;
; entities
;

(defrecord Member [id username first middle last])
(defrecord Address [id street1 street2 city state zipcode country gps-coords])
;(defrecord Project [id name tagline])

(defrecord LoginEvent [id])
(defrecord ClientState [name])

;
; relations
;

(defrecord MemberAddress [mid aid])
(defrecord ProjectMember [pid mid])

;
; behaviors
;

(defrule greet-member
         "Greet the members when they login"
         [LoginEvent (== id ?mid)]
         [Member (== id ?mid) (== username ?username)]
         [MemberAddress (== mid ?mid) (== aid ?aid)]
         [Address (== id ?aid)
          (== city ?city)]
         =>
         (rum/mount (greeting-view (str "Welcome " ?username " from " ?city))
                    (. js/document (getElementById "greeting-popup"))))

(defrule initial-state
         "Start State for client UI"
         [State (= name :start)]
         =>

         )



;(defrule logged-in-users)

;
; plumbing
;

(defsession session 'cs.core)
(defonce db (atom session))

(defn on-js-reload []
  ;; optionally touch your app-state to force rerendering depending on
  ;; your application
  ;; (swap! app-state update-in [:__figwheel_counter] inc)
  ;(js/alert "testing")
  (let [id "mid-member-id2!!!"
        aid "aid-address-id!!!"
        username "kahunamoore"
        first "Kenneth"
        last "Moore"
        middle "A"
        street1 "11916 Big Blue Road"
        street2 ""
        city "Nevada City"
        state "CA"
        zip "95959-1234"
        country "USA"
        gps "234+ 233+"]
    (-> @db
        (insert (->Member id username first middle last))
        (insert (->Address aid street1 street2 city state zip country gps))
        (insert (->MemberAddress id aid))
        (insert (->LoginEvent id))
        (fire-rules))))


(on-js-reload)
