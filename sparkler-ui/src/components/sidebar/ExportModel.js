import React, {Component} from "react";
import {Button} from "@blueprintjs/core";
import {connect} from "react-redux";
import {crawlStatus, exportData, fetchModel, killCrawl, startCrawl} from "../../actions";

class ExportData extends Component {

    constructor(props) {
        super(props);
        this.state = {}
        this.handleClick2 = this.handleClick2.bind(this);

    }

    handleClick2 = function () {
        this.props.exportModel(this.props.current_model)
    }

    render() {
        return (
            <Button style={{marginLeft: "10px"}} icon={"export"} text="Export Model" onClick={this.handleClick2}/>
        )
    }
}

const mapStateToProps = state => {
    return {
        current_model: state.modelreducer.current_model
    }
}


const mapDispatchToProps = dispatch => ({
    exportModel: (model) => dispatch(fetchModel(model)),

})


export default connect(mapStateToProps, mapDispatchToProps)(ExportData)
