import React, { Component } from 'react';
import {Col, Container, Row} from "react-grid-system";
import Banana from "../components/Banana";

class Home extends Component {

    render(){
        return(
            <Container fluid>
                <Row style={{marginTop:"10px"}}>
                    <Col sm={12}>
                        <Banana/>
                    </Col>
                </Row>
            </Container>
        )
    }
}

export default Home