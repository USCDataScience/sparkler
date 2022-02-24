import React, {Component} from 'react';
import {Col, Container, Row} from "react-grid-system";
import GenerateModel from "../components/sidebar/GenerateModel";
import CreateSeed from "../components/sidebar/CreateSeed";
import StartCrawl from "../components/sidebar/StartCrawl";
import ExportData from "../components/sidebar/ExportData";
import {Divider} from "@blueprintjs/core";
import Previews from "../components/Previews";
import CrawlConfig from "../components/sidebar/CrawlConfig";
import ExportModel from "../components/sidebar/ExportModel";


class Home extends Component {

    render() {
        return (
            <Container fluid>
                <Row style={{marginTop: "10px"}}>
                    <Col sm={3}>
                        <GenerateModel/>
                        <Divider/>
                        <CreateSeed/>
                        <CrawlConfig/>
                        <Divider/>
                        <StartCrawl/>
                        <Divider/>
                        <ExportData/>
                        <ExportModel/>
                    </Col>
                    <Col sm={9}>
                        <Previews/>
                    </Col>
                </Row>
            </Container>
        )
    }
}

export default Home
