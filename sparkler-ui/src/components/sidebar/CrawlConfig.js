import React, {Component} from 'react';
import {AnchorButton, H5} from "@blueprintjs/core";
import SeedURLDialog from "../dialogs/SeedURLDialog";
import {connect} from "react-redux";
import CrawlConfigDialog from "../dialogs/CrawlConfigDialog";


class CrawlConfig extends Component {


    constructor(props) {
        super(props);
        this.state = {
            open: false
        };
        this.modalElement = React.createRef();
    }

    handleOpen = () => this.modalElement.current.handleOpen();

    render() {
        return (
            <div>
                <H5>Crawler Configuration</H5>
                <AnchorButton disabled={!this.props.current_model} icon={"import"} onClick={this.handleOpen}
                              text="Crawler Configuration File"/>
                <CrawlConfigDialog ref={this.modalElement}/>
            </div>
        )
    }
}

const mapStateToProps = state => {
    return {
        current_model: state.modelreducer.current_model
    }
}

export default connect(mapStateToProps)(CrawlConfig)
