import React, { Component } from 'react';
import {Col, Container, Row} from "react-grid-system";
import Solr from "../components/Solr";

class Home extends Component {

    render(){
        return(
            <Container fluid>
                <Row style={{marginTop:"10px"}}>
                    <Col sm={12}>
                        <Solr/>
                    </Col>
                </Row>
            </Container>
        )
    }
}

export default Home
