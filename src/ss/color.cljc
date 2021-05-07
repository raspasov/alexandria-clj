(ns ss.color)

;3rd party
(def light-gray-bckgrn "#f0f0f0")
(def facebook-blue "#3e5895")
(def instagram-orange "#f0673e")
(def instagram-purple "#8a3ab9")

(def impress-purple "#b556ff")
(def intense-purple "#b556ff")
(def impress-gray "#b7b7b7")
(def impress-blue "#1f8eff")
(def impress-green "#8bc058")
(def impress-yellow-1 "#ffba00")

(def gold "#ffde9d")
(def silver "#e1dfdf")
(def rose-gold "#f8c0a6")



(defn -yellow!
 ([] (-yellow! 1))
 ([opacity]
  (str "rgba(253,219,41," opacity ")")))
(def yellow! (memoize -yellow!))


(defn -white
 ([] (-white 1))
 ([opacity]
  (str "rgba(255,255,255," opacity ")")))
(def wh (memoize -white))

(def t "transparent")

(defn -black
 ([] (-black 1))
 ([opacity]
  (str "rgba(0,0,0," opacity ")")))
(def bl (memoize -black))

(defn green
 ([] (green 1))
 ([opacity]
  (str "rgba(114,204,96," opacity ")")))
(def green-c (memoize green))

(defn green-2
 ([] (green-2 1))
 ([opacity]
  (str "rgba(75,149,66," opacity ")")))

(defn green-darker
 ([] (green-darker 1))
 ([opacity]
  (str "rgba(29,92,16," opacity ")")))

(defn orange-grdnt
 ([] (orange-grdnt 1))
 ([opacity]
  (str "rgba(249,97,13," opacity ")")))
(def orange-grdnt-c (memoize orange-grdnt))

(defn purple-grdnt
 ([] (purple-grdnt 1))
 ([opacity]
  (str "rgba(148,4,244," opacity ")")))
(def purple-grdnt-c (memoize purple-grdnt))

(defn light-gray
 ([] (light-gray 1))
 ([opacity]
  (str "rgba(240,240,240," opacity ")")))
(def light-gray-c (memoize light-gray))

(defn dark-gray
 ([] (dark-gray 1))
 ([opacity]
  (str "rgba(40,40,40," opacity ")")))
(def dark-gray-c (memoize dark-gray))

(defn gray []
 (str "rgba(157,157,164,1)"))

(defn darker-gray
 ([] (darker-gray 1))
 ([opacity]
  (str "rgba(28,28,30," opacity ")")))
(def darker-gray-c (memoize darker-gray))

(defn red-orange
 ([] (red-orange 1))
 ([opacity]
  (str "rgba(255,48,0," opacity ")")))
(def red-orange-c (memoize red-orange))

(def inactive-opacity 0.33)

(defn purple-deep-test-1
 ([] (purple-deep-test-1 1))
 ([opacity]
  (str "rgba(15,10,36," opacity ")")))

(defn rewards-pink
 ([] (rewards-pink 1))
 ([opacity]
  (str "rgba(240,80,90," opacity ")")))
(def rewards-pink-c (memoize rewards-pink))

(defn ios-blue
 ([] (ios-blue 1))
 ([opacity] (str "rgba(14,122,254," opacity ")")))
(def ios-blue-c (memoize ios-blue))

(def orange-2 "#ff3000")

(defn off-white-1
 ([] (off-white-1 1))
 ([opacity]
  (str "rgba(252,252,252," opacity ")")))

(defn shadow
 [{:keys [color opacity offset radius] :or {color (bl) opacity 0.7 offset {:height 0 :width 0} radius 8}}]
 {:shadowColor   color
  :shadowOpacity opacity
  :shadowRadius  radius
  :shadowOffset  offset})

