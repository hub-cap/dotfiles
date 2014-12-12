(require 'whitespace)
(setq-default whitespace-style '(face trailing lines empty indentation::space))
(setq-default whitespace-line-column 80)
(add-hook 'before-save-hook 'delete-trailing-whitespace)
(global-whitespace-mode 1)

;;Set the javascipt indent to 2 since its callback hell
(setq js-indent-level 2)
