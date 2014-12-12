(require 'scala-mode2)
(require 'ensime)

;(add-hook 'scala-mode-hook 'ensime-scala-mode-hook)
(add-to-list 'auto-mode-alist '("\.scala" . scala-mode2)
             '("\.sbt\'" . scala-mode2) )
