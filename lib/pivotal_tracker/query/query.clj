(ns pivotal-tracker.query)

(comment "
Searches can be refined with the following modifications:

\"String in Quotes\"
Quotes can be used to search for an exact phase
\"Your favorite string\"

label:labelName
You can also click on a label name to automatically search this way. If your label contains a SPACE surround it in quotes.
label:\"label name\"

type:storyType
where the story type can be Feature, Bug, Chore or Release
type:Feature

state:storyState
where storyState can be unstarted, started, finished, delivered, accepted, or rejected
state:unstarted

requester: userName
where userName is the full name, initials or part of the user's name 
requester:JTK

owner: userName
where userName is the full name, initials or part of the user's name 
owner:\"James T Kirk\"

mywork: userName
Shows an in-progress work for the given username. Similar to the \"My Work\" panel, but for any user.
mywork:Spock

id: storyID
where storyID is the ID of the story (which can be found at the end of the \"link to this story\" URL)
id:42

NOTE: Combine searches by simply stringing them together separated by a space. The results that match every parameter will be displayed. 
state:started requester:DD label:\"my stuff\" keyword
")


(defn spaces? [s]
  (.find (.matcher #"\s" s)))

(defmacro criteria [& names]
  `(do 
     ~@(for [n names]
	 `(defn ~n [s#]
	    (let [s# (if (spaces? s#) (str \" s# \") s#)]
	      (str ~(str n) ":" (java.net.URLEncoder/encode s#)))))))

(defmacro enums [setname & names]
  `(do
     ~@(for [n names]
	 `(def ~n ~(str setname ":" n)))
     (def ~setname #{~@names})))

(defn join [& criteria]
  (apply str (interpose "+" criteria)))
