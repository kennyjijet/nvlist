title = "NVList Script Reference"
project = "Script Reference"
style = true
template = true
not_luadoc = true

alias("since", {"release", modifiers={version="$1"}})
alias("deprecated", {"warning", modifiers={deprecated_since="$1"}})
