import React, {Component} from 'react';
import {Button, Classes, Dialog, Intent, TextArea, Tooltip} from "@blueprintjs/core";
import {saveSeedURLs} from "../../actions";
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
        if(this.props.seed_urls !== prevProps.seed_urls){
            this.handleClose()
        }
    }

    handleClose = () => this.setState({isOpen: false});
    handleOpen = () => this.setState({isOpen: true});

    handleChange(event) {
        this.setState({value: event.target.value});
    }

    handleOkay() {
        this.props.saveSeedURLs(this.props.current_model, this.state.value)
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
    saveSeedURLs: (m,s) => dispatch(saveSeedURLs(m,s))
})

const mapStateToProps = state => {
    return {
        current_model: state.modelreducer.current_model,
        seed_urls: state.modelreducer.seed_urls
    }
}

export default connect(mapStateToProps, mapDispatchToProps, null, {forwardRef: true})(SeedURLDialog)