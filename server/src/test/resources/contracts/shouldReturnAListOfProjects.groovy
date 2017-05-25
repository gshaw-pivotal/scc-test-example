package contracts

org.springframework.cloud.contract.spec.Contract.make {
    request {
        method 'GET'
        url '/projects'
    }
    response {
        status 200
        body("""
            [
                {
                    "pid": 555,
                    "project_name": "project_1"
                },
                {
                    "pid": 888,
                    "project_name": "project_2"
                }
            ]
        """)
    }
}
