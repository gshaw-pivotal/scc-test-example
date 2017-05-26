package contracts

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    priority 1
    request {
        method 'GET'
        url '/users/1'
    }
    response {
        status 200
        body(
            error: 'No user found'
        )
    }
}