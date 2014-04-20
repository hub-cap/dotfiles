;(setq-default c-basic-offset 4 c-default-style "linux")
;(setq-default tab-width 4 indent-tabs-mode t)
;(define-key c-mode-base-map (kbd "RET") 'newline-and-indent)
(electric-pair-mode) ; Make sure this mode is toggled on
(defun my-c-init-hook ()
  (define-key c-mode-base-map (kbd "RET") 'newline-and-indent))
(add-hook 'c-initialization-hook 'my-c-init-hook)

(setq-default c-basic-offset 4
              c-default-style "linux")
(setq-default tab-width 4
              indent-tabs-mode nil)
(defun my-c-mode-common-hook ()
  (c-toggle-hungry-state 1))
(add-hook 'c-mode-common-hook 'my-c-mode-common-hook)
