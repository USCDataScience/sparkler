import React, {Component} from 'react';
import {Button, Classes, Dialog, Intent, TextArea, Tooltip} from "@blueprintjs/core";
import {fetchSeeds, saveSeedURLs} from "../../actions";
import {connect} from "react-redux";

class SeedURLDialog extends Component {


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

    componentDidMount() {
    }

    handleClose = () => this.setState({isOpen: false});
    handleOpen = () => this.setState({isOpen: true});

    handleChange(event) {
        this.setState({value: event.target.value});
    }

    handleOkay() {
        this.props.saveSeedURLs(this.props.current_model, this.state.value)
        this.handleClose()
    }

    shouldComponentUpdate(nextProps, nextState, nextContext) {
        debugger;
        if (nextProps.current_model !== undefined && nextProps.current_model !== this.props.current_model) {
            this.props.fetchSeeds(nextProps.current_model);
            return true
        }
        if (this.props.seeds !== nextProps.seeds) {
            this.state.value = "";
            return true;
        }
        if (this.state.isOpen !== nextState.isOpen) {
            return true;
        }

        if (nextState.value !== this.state.value) {
            return true
        }

        return false
    }


    render() {
        return (
            <Dialog
                icon="search-text"
                onClose={this.handleClose}
                title="Seed URLs"
                {...this.state}
            >
                <div className={Classes.DIALOG_BODY} style={{minHeight: "200px"}}>
                    <TextArea style={{minHeight: "200px"}}
                              growVertically={true}
                              large={true}
                              intent={Intent.PRIMARY}
                              onChange={this.handleChange}
                              value={this.state.value}
                              fill={true}
                    />
                    <h5>Existing Seeds</h5>
                    <TextArea growVertically={true}
                              large={true}
                              intent={Intent.PRIMARY} disabled={true} fill={true} value={this.props.seeds}/>
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
    fetchSeeds: (m) => dispatch(fetchSeeds(m))
})

const mapStateToProps = state => {
    return {
        current_model: state.modelreducer.current_model,
        seed_urls: state.modelreducer.seed_urls,
        seeds: state.modelreducer.seeds
    }
}

export default connect(mapStateToProps, mapDispatchToProps, null, {forwardRef: true})(SeedURLDialog)
