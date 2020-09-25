import React, {Component} from 'react';
import {Button, Classes, Dialog, Intent, TextArea, Tooltip} from "@blueprintjs/core";
import {fetchConfig, saveSeedURLs, updateCrawlConfig} from "../../actions";
import {connect} from "react-redux";

class CrawlConfigDialog extends Component {


    constructor(props) {
        super(props);
        this.state = {
            value: ''
        }
        this.handleChange = this.handleChange.bind(this);
        this.handleOkay = this.handleOkay.bind(this);
    }

    componentDidUpdate(prevProps, prevState, snapshot) {
        if (this.props.seed_urls !== prevProps.seed_urls) {
            this.handleClose()
        }
    }

    shouldComponentUpdate(nextProps, nextState, nextContext) {
        if (nextProps.current_model !== this.props.current_model) {
            this.props.fetchConfig(nextProps.current_model)
        }
        if (this.props.current_config !== nextProps.current_config) {
            this.setState({value: JSON.stringify(nextProps.current_config, null, 2)});
            return true;
        }

        if (this.state.value !== nextState.value) {
            return true
        }
        if (this.state.isOpen !== nextState.isOpen) {
            return true;
        }

        return false
    }

    handleClose = () => {
        this.setState({isOpen: false});

    }
    handleOpen = () => {
        this.setState({isOpen: true});

    }

    handleChange(event) {
        this.setState({value: event.target.value});
    }

    handleOkay() {
        this.props.updateCrawlConfig(this.props.current_model, JSON.parse(this.state.value))
        this.handleClose()
    }


    render() {

        return (
            <Dialog
                icon="search-text"
                onClose={this.handleClose}
                title="Crawler Config"
                style={{width: "900px"}}
                {...this.state}
            >
                <div className={Classes.DIALOG_BODY} style={{minHeight: "500px", minWidth: "800px"}}>
                    <TextArea style={{minHeight: "500px", minWidth: "800px"}}
                              growVertically={true}
                              large={true}
                              intent={Intent.PRIMARY}
                              onChange={this.handleChange}
                              value={this.state.value}
                              fill={true}
                    />
                </div>
                <div className={Classes.DIALOG_FOOTER}>
                    <div className={Classes.DIALOG_FOOTER_ACTIONS}>
                        <Tooltip content="This button is hooked up to close the dialog.">
                            <Button onClick={this.handleClose}>Cancel</Button>
                        </Tooltip>
                        <Button
                            intent={Intent.PRIMARY}
                            onClick={this.handleOkay}
                        >
                            Save
                        </Button>
                    </div>
                </div>
            </Dialog>
        )
    }
}

const mapDispatchToProps = dispatch => ({
    saveSeedURLs: (m, s) => dispatch(saveSeedURLs(m, s)),
    fetchConfig: (m) => dispatch(fetchConfig(m)),
    updateCrawlConfig: (m, c) => dispatch(updateCrawlConfig(m, c))
});

const mapStateToProps = state => {
    return {
        current_model: state.modelreducer.current_model,
        current_config: state.modelreducer.current_config
    }
};

export default connect(mapStateToProps, mapDispatchToProps, null, {forwardRef: true})(CrawlConfigDialog)
