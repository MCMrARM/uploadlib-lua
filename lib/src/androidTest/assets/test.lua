uploader.register({
    name = "Test Uploader Script",
    loginSupported = true
})

jsonTest = json.decode("{\"test\":[{\"test\":\"test\"},{\"number\":2.5}]}")
print(jsonTest.test[2].number)
print("Json " .. json.encode(jsonTest))

function uploader.login(controller)
    print("Test!")
    controller:setLoadingState()

    resp = http.post({
        url = "http://httpbin.org/post",
        -- body = http.body("text/plain; charset=utf-8", "This is a test!"),
        body = http.jsonBody({
            test = {
                array = {1, 2, 3, 4, 5},
                text = "An example"
            }
        }),
        headers = {
            ["User-Agent"] = "test",
            ["Accept"] = {"application/json", "text/plain"}
        }
    })
    print("Server header: " .. resp:header("Server"))
    print("Headers: " .. json.encode(resp:allHeaders()))
    print(json.encode(resp:jsonBody()))

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