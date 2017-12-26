uploader.register({
    name = "Test Uploader Script",
    loginSupported = true
})

function uploader.login(controller)
    print("Test!")
    controller:setLoadingState()

    resp = http.post({
        url = "http://httpbin.org/post",
        body = http.body("text/plain; charset=utf-8", "This is a test!"),
        headers = {
            ["User-Agent"] = "test",
            ["Accept"] = {"application/json", "text/plain"}
        }
    })
    print(resp:stringBody())

    controller:setWebState({
        url = "https://example.com/",
        loadUrl = true,
        onPageFinished = function(browser, url)
            print("Page loaded: " .. url)
            if url == "https://www.iana.org/domains/reserved" then
                browser:finish()
            end
        end
    })

    controller:setLoadingState()
    os.execute("sleep 1")

    browser = controller:createWebBrowser()
    browser:loadUrl("https://example.com/")
    browser.onPageFinished = function(browser, url)
        print("Page loaded: " .. url)
        if url == "https://www.iana.org/domains/reserved" then
            browser:finish()
        end
    end
    controller:setWebState(browser)

    controller:setLoadingState()
    os.execute("sleep 2")
end