import React, {Component} from 'react';
import {Button, Classes, Dialog, FormGroup, InputGroup, Intent, TextArea, Tooltip} from "@blueprintjs/core";
import {fetchConfig, saveSeedURLs, startCrawl, updateCrawlConfig} from "../../actions";
import {connect} from "react-redux";

class CrawlConfigDialog extends Component {


    constructor(props) {
        super(props);
        this.state = {
            value: '',
            iterations: 1,
            topgroups: undefined,
            topn: undefined
        }
        this.handleChange = this.handleChange.bind(this);
        this.handleOkay = this.handleOkay.bind(this);


    }

    componentDidUpdate(prevProps, prevState, snapshot) {


    }

    shouldComponentUpdate(nextProps, nextState, nextContext) {

        return true
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
        let obj = {}
        if (this.state.iterations !== undefined) {
            obj["iterations"] = this.state.iterations
        }
        if (this.state.topgroups !== undefined) {
            obj["topgroups"] = this.state.topgroups
        }
        if (this.state.topn !== undefined) {
            obj["topn"] = this.state.topn
        }
        this.props.startCrawl(this.props.current_model, obj);
        this.handleClose()
    }

    handleItChange(event) {
        this.setState({iterations: event.target.value});
    }

    handleGroupChange(event) {
        this.setState({topgroups: event.target.value});
    }

    handleTopNChange(event) {
        this.setState({topn: event.target.value});
    }

    render() {

        return (
            <Dialog
                icon="search-text"
                onClose={this.handleClose}
                title="Start Crawl"
                {...this.state}
            >
                <div className={Classes.DIALOG_BODY}>
                    <FormGroup
                        helperText={"How many iterations. (-1 for all URLs)"}
                        inline={false}
                        label={"Iterations"}
                        labelFor="text-input"
                    >
                        <InputGroup id="text-input" onChange={this.handleItChange.bind(this)}
                                    value={this.state.iterations}/>
                    </FormGroup>
                    <FormGroup
                        helperText={"Max groups to be selected for fetch"}
                        inline={false}
                        label={"Top Groups"}
                        labelFor="text-input"
                    >
                        <InputGroup id="text-input" placeholder={"Leave Blank for Default"}
                                    onChange={this.handleGroupChange.bind(this)} value={this.state.topgroups}/>
                    </FormGroup>
                    <FormGroup
                        helperText={"Top URLs per domain to be selected for a round"}
                        inline={false}
                        label={"Top URLs"}
                        labelFor="text-input"
                    >
                        <InputGroup id="text-input" placeholder={"Leave Blank for Default"}
                                    onChange={this.handleTopNChange.bind(this)} value={this.state.topn}/>
                    </FormGroup>
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
                            Run!
                        </Button>
                    </div>
                </div>
            </Dialog>
        )
    }

}

const mapDispatchToProps = dispatch => ({
    startCrawl: (model, opts) => dispatch(startCrawl(model, opts)),

})

const mapStateToProps = state => {
    return {
        current_model: state.modelreducer.current_model,
        current_config: state.modelreducer.current_config
    }
}

export default connect(mapStateToProps, mapDispatchToProps, null, {forwardRef: true})(CrawlConfigDialog)
