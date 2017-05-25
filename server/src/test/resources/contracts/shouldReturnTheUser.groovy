package contracts

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    request {
        method 'GET'
        url '/users/99'
    }
    response {
        status 200
        body([
            id: '99',
            name: 'a_single_user'
        ])
    }
}