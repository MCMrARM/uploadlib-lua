uploader.register({
    name = "Test Uploader Script",
    loginSupported = true
})

function uploader.upload(file)
    resp = http.post({
        url = "http://httpbin.org/post",
        body = http.body(file)
    })
    print(resp:stringBody())
end