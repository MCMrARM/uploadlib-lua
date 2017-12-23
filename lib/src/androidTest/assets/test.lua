uploader.register({
    name = "Test Uploader Script",
    loginSupported = true
})

function uploader_login(controller)
    controller:setLoadingState()
    os.execute("sleep 10")
end