uploader.register({
    name = "Test Uploader Script",
    loginSupported = true
})

function uploader.login(controller)
    print("Test!")
    controller:setLoadingState()
    os.execute("sleep 2")
    controller:setWebState({
        url = "https://example.com/",
        loadUrl = true,
        onPageFinished = function(browser, url)
            print("Page loaded: " .. url)
        end
    })
end