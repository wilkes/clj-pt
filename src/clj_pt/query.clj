(ns clj-pt.query
  (:import (java.net URLEncoder)))

(defn encode [s]
  (URLEncoder/encode (if (some #(Character/isWhitespace %) s)
				(str \" s \")
				s)))

(defmacro criteria [& names]
  `(do
     ~@(map (fn [n] `(defn ~n [s#]
		     (str ~(str n) ":" (encode s#))))
	  names)))

(defmacro enums 
  "Defines a set enumerated filters where all the values are storied as a set 
   in the first arg."
  [setname & names]
  `(do
     ~@(map (fn [n] `(def ~n ~(str setname ":" n))) names)
     (def ~(symbol (str "pt-" setname)) #{~@names})))

(defn combine
  "Combine multiple filters to use in the search"
  [& filters]
  (apply str (interpose "+" filters)))

