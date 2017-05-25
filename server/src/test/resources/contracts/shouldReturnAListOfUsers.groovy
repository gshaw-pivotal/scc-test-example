package contracts

org.springframework.cloud.contract.spec.Contract.make {
    request {
        method 'GET'
        url '/users'
    }
    response {
        status 200
        body("""
            [
                {
                    "id": 1234,
                    "name": "thename"
                },
                {
                    "id": 4567,
                    "name": "anothername"
                }
            ]
        """)
    }
}
