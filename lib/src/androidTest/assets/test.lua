uploader.register({
    name = "Test Uploader Script",
    loginSupported = true
})

function uploader.login(controller)
    controller:setLoadingState()
    os.execute("sleep 10")
end