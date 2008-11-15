;; This script is run by the Ant build task to precompile the core
;; Clojure source files.

(println "Compiling Clojure core sources...")

(binding [*compile-path* (System/getProperty "clojure.compile.path")]
  (compile 'pivotal-tracker))

