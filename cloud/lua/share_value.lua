local _M = {}
local cmd = ""
function _M.sendCmd(value)
    cmd = value
end

function _M.getCmd()
    return cmd
end

return _M