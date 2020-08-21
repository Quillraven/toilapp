import React, {Component} from 'react';
import CustomTable from "./Table/Table";
import Card from "./Card/Card"
import GridItem from "./Grid/GridItem"
import CardHeader from "./Card/CardHeader";
import CardBody from "./Card/CardBody";

interface User {
    id: string
    name: string
    email: string
}

interface Comment {
    user: User
    date: Date
    text: string
}

interface Toilet {
    title: string
    location: string
    preview: string
    rating: number
    disable: boolean
    toiletCrewApproved: boolean
    description: string
    comments: Array<Comment>
    images: Array<string>
}

interface ToiletListProps {
}

interface ToiletListState {
    Toilets: Array<Toilet>;
    isLoading: boolean;
}

class ToiletList extends Component<ToiletListProps, ToiletListState> {

    constructor(props: ToiletListProps) {
        super(props);

        this.state = {
            Toilets: [],
            isLoading: false
        };
    }

    async componentDidMount() {
        this.setState({isLoading: true});

        const response = await fetch('http://localhost:3000/api/toilets');
        const data = await response.json();
        this.setState({Toilets: data, isLoading: false});
    }

    render() {
        const {Toilets, isLoading} = this.state;

        if (isLoading) {
            return <p>Fetching toilets...</p>;
        }

        return (
            <GridItem>
                <Card>
                    <CardHeader color={"danger"}>
                        <h4 className={"cardTitle"}>Toilets</h4>
                    </CardHeader>
                    <CardBody>
                        <CustomTable
                            tableHeaderColor={"primary"}
                            tableHead={["Title", "Location", "Rating"]}
                            tableData={Toilets.map((toilet: Toilet) => (
                                [`${toilet.title}`, `${toilet.location}`, `${toilet.rating}`]
                            ))}
                        />
                    </CardBody>
                </Card>
            </GridItem>
        );
    }
}

export default ToiletList;
