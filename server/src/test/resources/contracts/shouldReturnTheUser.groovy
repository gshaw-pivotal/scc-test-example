package contracts

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    priority 2
    request {
        method 'GET'
        //Consumers can be any value, producers must be a real fixed value on the request side
        url value(consumer(regex('/users/[0-9]{1,}')), producer('/users/99'))
    }
    response {
        status 200
        body([
            //Consumers must be a real fixed value, producers can be any value on the response side
            id: value(consumer('123'), producer(regex('[0-9]{1,}'))),
            name: 'a_single_user'
        ])
    }
}