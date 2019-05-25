import * as React from "react";
import * as PropTypes from "prop-types";
import { ThemeType } from 'react-uwp/Theme';

export default class Field extends React.Component {
    
    render() {
        const { name, value, style } = this.props
        return (
            <div style={{
                display: 'inline-block',
                ...style
            }}>
                <span style={{marginRight: 10, fontWeight: 'bold'}}>
                    {name}:
                </span>
                {value}
            </div>
        )
    }
}

class ColorText extends React.Component {
    
    render() {
        const { color, children, style } = this.props
        return (
            <div style={{
                display: 'inline-block',
                borderRadius: 5,
                backgroundColor: color,
                padding: '0px 5px',
                ...style
            }}>
                {children}
            </div>
        )
    }
}

export {
    ColorText,
}