import React, {Component} from 'react';
import {Button, H5} from "@blueprintjs/core";
import {connect} from "react-redux";
import {crawlStatus, killCrawl, startCrawl} from "../../actions";
import {CRAWL_FINISHED, CRAWL_STARTED, CRAWL_STARTING} from "../../reducers/types";


class StartCrawl_old extends Component {


    constructor(props) {
        super(props);
        this.handleClick = this.handleClick.bind(this);
        this.handleClick2 = this.handleClick2.bind(this);
        this.check_status = this.check_status.bind(this)

    }

    check_status() {
        let that = this;
        (function loop() {
            setTimeout(function () {
                // execute script
                debugger;
                if (that.props.crawl_status !== CRAWL_FINISHED) {

                    that.props.crawlStatus(that.props.current_model)
                    loop()
                }
            }, 9000);
        }());
    }

    check_status2() {
        setInterval(function () {
            this.props.crawlStatus(this.props.current_model)
        }, 5000);
    }

    componentWillUpdate(nextProps, nextState, nextContext) {
        if (nextProps.crawl_status === CRAWL_STARTING || nextProps.crawl_status === CRAWL_STARTED) {
            this.check_status()
        }
    }


    handleClick = function () {
        this.props.startCrawl(this.props.current_model)

    }

    handleClick2 = function () {
        this.props.killCrawl(this.props.current_model)

    }

    render() {
        return (
            <div>
                <H5>Start the Crawl</H5>
                <Button
                    disabled={!this.props.current_model || this.props.crawl_status === CRAWL_STARTING || this.props.crawl_status === CRAWL_STARTED}
                    icon={"time"} text="Start Crawler" onClick={this.handleClick}/>
                <br/>
                <Button
                    disabled={!this.props.current_model || !(this.props.crawl_status === CRAWL_STARTING || this.props.crawl_status === CRAWL_STARTED)}
                    icon={"time"} text="Kill Crawl" onClick={this.handleClick2}/>

            </div>
        )
    }
}

const mapStateToProps = state => {
    return {
        current_model: state.modelreducer.current_model,
        crawl_status: state.modelreducer.crawl_status
    }
}

const mapDispatchToProps = dispatch => ({
    startCrawl: (model) => dispatch(startCrawl(model)),
    crawlStatus: (model) => dispatch(crawlStatus(model)),
    killCrawl: (model) => dispatch(killCrawl(model))
})

export default connect(mapStateToProps, mapDispatchToProps)(StartCrawl_old)
