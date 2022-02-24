import React, {Component} from 'react'
import {Button, Card, Classes, Elevation, H5} from "@blueprintjs/core";
import {Col, Container, Row} from "react-grid-system";
import {connect} from "react-redux";
import {setRelevancy} from "../actions";

const Example = ({data}) => <img style={{maxWidth: "100%", height: "100%"}} src={`data:image/jpeg;base64,${data}`}/>

class IFrameFrame extends Component {


    constructor(props) {
        super(props);

        this.state = {
            title: "",
            url: "",
            skeleton: true,
            hr_btn_class: false,
            r_btn_class: false,
            nr_btn_class: false
        }
        this.updaterelevancy = this.updaterelevancy.bind(this);

    }

    updaterelevancy = function (val, which) {
        debugger;
        this.props.setRelevancy(this.props.iframe, val)
        if (which === "hr") {
            this.setState({hr_btn_class: true});
            this.setState({r_btn_class: false});
            this.setState({nr_btn_class: false});
        } else if (which === "r") {
            this.setState({hr_btn_class: false});
            this.setState({r_btn_class: true});
            this.setState({nr_btn_class: false});
        } else {
            this.setState({hr_btn_class: false});
            this.setState({r_btn_class: false});
            this.setState({nr_btn_class: true});
        }
    }


    launchPreview = () => {
        window.open(this.props.url, "_blank")
    }

    render() {
        return (

            <Card style={{height: "470px"}} interactive={true} elevation={Elevation.TWO}>
                <Container fluid>
                    <Row>
                        <Col sm={12}>
                            <a href={this.props.url} target="_blank"><H5
                                className={this.props.skeleton ? Classes.SKELETON : ''}>Title: {this.props.title}</H5></a>
                            <a href={this.props.url} target="_blank"><H5
                                style={{textOverflow: "ellipsis", whiteSpace: "nowrap", overflow: "hidden"}}
                                className={this.props.skeleton ? Classes.SKELETON : ''}>URL: {this.props.url}</H5></a>
                        </Col>
                    </Row>
                    <Row className={this.props.skeleton ? Classes.SKELETON : ''}
                         style={{marginTop: "10px", height: "300px"}}>
                        <Col sm={12}>
                            <Example data={this.props.image}/>
                        </Col></Row>
                    <br/>

                    <Row>
                        <Col sm={12}>
                            <Button onClick={() => this.updaterelevancy(2, "hr")}
                                    className={`${this.props.skeleton ? Classes.SKELETON : ''} ${this.state.hr_btn_class ? Classes.ACTIVE : null}`}>Highly
                                Relevant</Button>
                            <Button onClick={() => this.updaterelevancy(1, "r")}
                                    className={`${this.props.skeleton ? Classes.SKELETON : ''} ${this.state.r_btn_class ? Classes.ACTIVE : null}`}>Relevant</Button>
                            <Button onClick={() => this.updaterelevancy(0, "nr")}
                                    className={`${this.props.skeleton ? Classes.SKELETON : ''} ${this.state.nr_btn_class ? Classes.ACTIVE : null}`}>Not
                                Relevant</Button>
                        </Col>
                    </Row>
                </Container>

            </Card>
        )
    }
}

const mapStateToProps = state => {
    return {
        skeleton: state.searchreducer.running
    }
}

const mapDispatchToProps = dispatch => ({
    setRelevancy: (a, v) => dispatch(setRelevancy(a, v))
})


export default connect(mapStateToProps, mapDispatchToProps)(IFrameFrame)
