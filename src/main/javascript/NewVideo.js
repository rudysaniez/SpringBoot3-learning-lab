import React from "react";

class NewVideo extends React.Component {

    constructor(props) {
        super(props);
        this.state={name:""};

    }

    handleChange(event) {
        this.setState({name: event.target.value});
    }

    async handleSubmit(event) {
        event.preventDefault();
        await fetch ("/videos", {
            method: "POST",
            headers: {
                "Content-type": "application/json"
            },
            body: JSON.stringify({name: this.state.name})
        }).then(response => window.location.href = "/react")
    }

    render() {
        return (
            <form onSubmit={this.handleSubmit}>
                <input type="text" value={this.state.name}
                onChange={this.handleChange}/>

                <button type="submit">Submit</button>
            </form>
        )
    }
}
export default NewVideo