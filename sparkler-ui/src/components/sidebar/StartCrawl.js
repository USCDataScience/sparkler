import React, {Component} from 'react';
import {AnchorButton, Button, H5} from "@blueprintjs/core";
import SeedURLDialog from "../dialogs/SeedURLDialog";
import {connect} from "react-redux";
import StartCrawlDialog from "../dialogs/StartCrawlDialog";
import {CRAWL_STARTED, CRAWL_STARTING} from "../../reducers/types";


class StartCrawl extends Component {


    constructor(props) {
        super(props);
        this.state = {
            open: false
        };
        this.modalElement = React.createRef();
    }

    handleOpen = () => this.modalElement.current.handleOpen();
    handleClick2 = function () {
        this.props.killCrawl(this.props.current_model)

    }

    render() {
        return (
            <div>
                <H5>Run Crawl</H5>
                <AnchorButton disabled={!this.props.current_model} icon={"import"} onClick={this.handleOpen}
                              text="Start Crawl"/>
                <Button style={{marginLeft: "10px"}}
                        disabled={!this.props.current_model || !(this.props.crawl_status === CRAWL_STARTING || this.props.crawl_status === CRAWL_STARTED)}
                        icon={"time"} text="Kill Crawl" onClick={this.handleClick2}/>
                <StartCrawlDialog ref={this.modalElement}/>
            </div>
        )
    }
}

const mapStateToProps = state => {
    return {
        current_model: state.modelreducer.current_model
    }
}

export default connect(mapStateToProps)(StartCrawl)
