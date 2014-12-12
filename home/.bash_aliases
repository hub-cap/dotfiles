alias emacs="emacs -nw"

export GOPATH=$HOME/Documents/golang/gopath
export EDITOR=emacs
. "$HOME/.homesick/repos/homeshick/homeshick.sh"
export TERM='xterm-256color'
export SCALA_HOME=/usr/local/share/scala/2.10.4
export PATH=$PATH:$SCALA_HOME/bin

export PATH=$PATH:/usr/local/go/bin
export PATH=$PATH:$HOME/Documents/golang/gopath/bin

# venvwrapper stuff
export WORKIN_HOME=$HOME/.virtualenvs
source /etc/bash_completion.d/virtualenvwrapper

# ssh for shared
alias sshs='ssh -i ~/.ssh/shared_rsa'

# bootstrap
function strap { sudo LANG=C.UTF-8 chroot ~/.bootstraps/$1/ /bin/bash; }
