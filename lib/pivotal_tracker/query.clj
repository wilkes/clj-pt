(ns pivotal-tracker.query
  (:import (java.net URLEncoder)))

(defn encode [s]
  (URLEncoder/encode (if (some #(Character/isWhitespace %) s)
				(str \" s \")
				s)))

(defmacro criteria [& names]
  `(do 
     ~@(for [n names]
	 `(defn ~n [s#]
	    (str ~(str n) ":" (encode s#))))))

(defmacro enums 
  "Defines a set enumerated filters where all the values are storied as a set 
   in the first arg."
  [setname & names]
  `(do
     ~@(for [n names]
	 `(def ~n ~(str setname ":" n)))
     (def ~setname #{~@names})))

(defn combine
  "Combine multiple filters to use in the search"
  [& filters]
  (apply str (interpose "+" filters)))

