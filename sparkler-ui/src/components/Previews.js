import React, {Component} from 'react'
import {connect} from "react-redux";
import IFrameFrame from "./IFrameFrame";
import {Col, Container, Row} from "react-grid-system";


class Previews extends Component {


    static render_nowebsites() {
        return (
            <Container>
                <Row><Col xs={12}>Generating Previews - This May Take A Few Minutes</Col></Row>
                <Row>
                    <Col sm={4}>
                        <IFrameFrame/>
                    </Col>
                    <Col sm={4}>
                        <IFrameFrame/>
                    </Col>
                    <Col sm={4}>
                        <IFrameFrame/>
                    </Col>
                </Row>
                <br/>
                <Row>
                    <Col sm={4}>
                        <IFrameFrame/>
                    </Col>
                    <Col sm={4}>
                        <IFrameFrame/>
                    </Col>
                    <Col sm={4}>
                        <IFrameFrame/>
                    </Col>
                </Row>
                <br/>
                <Row>
                    <Col sm={4}>
                        <IFrameFrame/>
                    </Col>
                    <Col sm={4}>
                        <IFrameFrame/>
                    </Col>
                    <Col sm={4}>
                        <IFrameFrame/>
                    </Col>
                </Row>
                <br/>
                <Row>
                    <Col sm={4}>
                        <IFrameFrame/>
                    </Col>
                    <Col sm={4}>
                        <IFrameFrame/>
                    </Col>
                    <Col sm={4}>
                        <IFrameFrame/>
                    </Col>
                </Row>
            </Container>
        )
    }

    static listToMatrix(list, elementsPerSubArray) {
        var matrix = [], i, k;

        for (i = 0, k = -1; i < list.length; i++) {
            if (i % elementsPerSubArray === 0) {
                k++;
                matrix[k] = [{pos: i + 1, obj: {url: "", title: ""}}];
            }

            matrix[k].push({pos: i + 1, obj: list[i]});
        }

        return matrix;
    }

//var marker = get_marker(jdata[i - 1]['label']);
    render_websites() {
        let arr = [];
        let matrix = Previews.listToMatrix(this.props.websites, 3)
        console.log(matrix)
        for (let i = 0; i < matrix.length; i++) {
            if (matrix[i][1] === undefined) {
                matrix[i][1] = {}
                matrix[i][1].obj = {image: "", url: "", title: ""}
                matrix[i][1].obj = {image: "", url: "", title: ""}
                matrix[i][1].obj = {image: "", url: "", title: ""}

            }
            if (matrix[i][2] === undefined) {
                matrix[i][2] = {}
                matrix[i][2].obj = {image: "", url: "", title: ""}
                matrix[i][2].obj = {image: "", url: "", title: ""}
                matrix[i][2].obj = {image: "", url: "", title: ""}

            }
            if (matrix[i][3] === undefined) {
                matrix[i][3] = {}
                matrix[i][3].obj = {image: "", url: "", title: ""}
                matrix[i][3].obj = {image: "", url: "", title: ""}
                matrix[i][3].obj = {image: "", url: "", title: ""}

            }
            if (matrix[i][1] && matrix[i][1].obj.image === undefined) {
                matrix[i][1].obj.image = "";
            }
            if (matrix[i][1] && matrix[i][1].obj.url === undefined) {
                matrix[i][1].obj.url = "";
            }
            if (matrix[i][1] && matrix[i][1].obj.title === undefined) {
                matrix[i][1].obj.title = "";
            }

            if (matrix[i][2] && matrix[i][2].obj.image === undefined) {
                matrix[i][2].obj.image = "";
            }
            if (matrix[i][2] && matrix[i][2].obj.url === undefined) {
                matrix[i][2].obj.url = "";
            }
            if (matrix[i][2] && matrix[i][2].obj.title === undefined) {
                matrix[i][2].obj.title = "";
            }

            if (matrix[i][3] && matrix[i][3].obj.image === undefined) {
                matrix[i][3].obj.image = "";
            }
            if (matrix[i][3] && matrix[i][3].obj.url === undefined) {
                matrix[i][3].obj.url = "";
            }
            if (matrix[i][3] && matrix[i][3].obj.title === undefined) {
                matrix[i][3].obj.title = "";
            }

            arr.push(<Row><Col sm={4}><IFrameFrame image={matrix[i][1].obj.image}
                                                   iframe={"page" + matrix[i][1].pos + "-ann"}
                                                   url={matrix[i][1].obj.url} title={matrix[i][1].obj.title}/></Col>
                <Col sm={4}><IFrameFrame image={matrix[i][2].obj.image} iframe={"page" + matrix[i][2].pos + "-ann"}
                                         url={matrix[i][2].obj.url} title={matrix[i][2].obj.title}/>
                </Col><Col sm={4}><IFrameFrame image={matrix[i][3].obj.image}
                                               iframe={"page" + matrix[i][3].pos + "-ann"} url={matrix[i][3].obj.url}
                                               title={matrix[i][3].obj.title}/></Col>
            </Row>)
        }
        return arr;
    }


    render() {
        if (this.props.current_model === undefined) {
            return (<div><h1>Please select or create a model</h1></div>)
        } else if (this.props.running) {
            return (Previews.render_nowebsites())
        } else if (this.props.websites === undefined) {
            return (<div/>)
        } else {
            return (this.render_websites())
        }
    }
}


const mapStateToProps = state => {
    return {
        websites: state.searchreducer.websites,
        running: state.searchreducer.running,
        current_model: state.modelreducer.current_model

    }
}


export default connect(mapStateToProps)(Previews)
