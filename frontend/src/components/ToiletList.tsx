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

interface GeoPoint {
    x: number
    y: number
}

interface Toilet {
    title: string
    location: GeoPoint
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
    image: any,
    Toilets: Array<Toilet>;
    isLoading: boolean;
}

class ToiletList extends Component<ToiletListProps, ToiletListState> {

    constructor(props: ToiletListProps) {
        super(props);

        this.state = {
            image: null,
            Toilets: [],
            isLoading: false
        };
    }

    async componentDidMount() {
        this.setState({isLoading: true});

        const responseToilets = await fetch('http://localhost:3000/api/toilets');
        const toiletData: Toilet[] = await responseToilets.json();

        this.setState({Toilets: toiletData, isLoading: false});
    }

    async getImage() {
        fetch('http://localhost:3000/api/previews/5f4a65c642ce3b12422f66ab')
            .then(responsePreview => responsePreview.json())
            .then(data => {
                console.log(`ImageData: ${data}`)
                this.setState({image: data})
            })
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
                        <img src={"data:image/png;base64," + this.getImage()} alt="test"/>
                        <CustomTable
                            tableHeaderColor={"primary"}
                            tableHead={["Title", "Location", "Rating"]}
                            tableData={Toilets.map((toilet: Toilet) => (
                                [
                                    `${toilet.title}`,
                                    `[${toilet.location.x},${toilet.location.y}]`,
                                    `${toilet.rating}`
                                ]
                            ))}
                        />
                    </CardBody>
                </Card>
            </GridItem>
        );
    }
}

export default ToiletList;
